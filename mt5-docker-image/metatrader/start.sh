#!/bin/bash

# Configuration variables
mt5file='/config/.wine/drive_c/Program Files/MetaTrader 5/terminal64.exe'
WINEPREFIX='/config/.wine'
WINEDEBUG='-all'
wine_executable="wine"
metatrader_version="5.0.37"
mt5server_port="8001"
#MT5_CMD_OPTIONS="/config:Z:\\metatrader\config.ini"
mono_url="https://dl.winehq.org/wine/wine-mono/10.3.0/wine-mono-10.3.0-x86.msi"
python_url="https://www.python.org/ftp/python/3.10.0/python-3.10.0.exe"
mt5setup_url="https://download.mql5.com/cdn/web/metaquotes.software.corp/mt5/mt5setup.exe"

# Function to display a graphical message
show_message() {
    echo $1
}

# Function to check if a dependency is installed
check_dependency() {
    if ! command -v $1 &> /dev/null; then
        echo "$1 is not installed. Please install it to continue."
        exit 1
    fi
}

# Function to check if a Python package is installed
is_python_package_installed() {
    python3 -c "import pkg_resources; exit(not pkg_resources.require('$1'))" 2>/dev/null
    return $?
}

# Function to check if a Python package is installed in Wine
is_wine_python_package_installed() {
    $wine_executable python -c "import pkg_resources; exit(not pkg_resources.require('$1'))" 2>/dev/null
    return $?
}

# Check for necessary dependencies
check_dependency "curl"
check_dependency "$wine_executable"

# Install Mono if not present
if [ ! -e "/config/.wine/drive_c/windows/mono" ]; then
    show_message "[1] Downloading and installing Mono..."
    curl -o /config/.wine/drive_c/mono.msi $mono_url
    WINEDLLOVERRIDES=mscoree=d $wine_executable msiexec /i /config/.wine/drive_c/mono.msi /qn
    rm /config/.wine/drive_c/mono.msi
    show_message "[1] Mono installed."
else
    show_message "[1] Mono is already installed."
fi

# Check if MetaTrader 5 is already installed
if [ -e "$mt5file" ]; then
    show_message "[2] File $mt5file already exists."
else
    show_message "[2] File $mt5file is not installed. Installing..."

    # Set Windows 10 mode in Wine and download and install MT5
    $wine_executable reg add "HKEY_CURRENT_USER\\Software\\Wine" /v Version /t REG_SZ /d "win10" /f
    show_message "[2] Downloading MT5 installer..."
    curl -o /config/.wine/drive_c/mt5setup.exe $mt5setup_url
    show_message "[2] Installing MetaTrader 5..."
    $wine_executable "/config/.wine/drive_c/mt5setup.exe" "/auto" &
    wait
    rm -f /config/.wine/drive_c/mt5setup.exe
fi

# Recheck if MetaTrader 5 is installed
if [ -e "$mt5file" ]; then
    show_message "[4] File $mt5file is installed. Running MT5..."
    $wine_executable "$mt5file" $MT5_CMD_OPTIONS &
else
    show_message "[4] File $mt5file is not installed. MT5 cannot be run."
fi

# Install Python in Wine if not present
if ! $wine_executable python --version 2>/dev/null; then
    show_message "[5] Installing Python in Wine..."
    curl -L $python_url -o /tmp/python-installer.exe
    $wine_executable /tmp/python-installer.exe /quiet InstallAllUsers=1 PrependPath=1
    rm /tmp/python-installer.exe
    show_message "[5] Python installed in Wine."
else
    show_message "[5] Python is already installed in Wine."
fi

# Upgrade pip and install required packages
show_message "[6] Installing Python libraries"
$wine_executable python -m pip install --upgrade --no-cache-dir pip

# Downgrade NumPy to version 1.24.3 for compatibility with MetaTrader5 library
if ! is_wine_python_package_installed "numpy==1.24.3"; then
    show_message "[6] Downgrading NumPy to version 1.24.3 for compatibility with MetaTrader5 library"
    $wine_executable python -m pip install --no-cache-dir numpy==1.24.3 --force-reinstall   
fi

show_message "[7] Installing MetaTrader5 library in Windows"
if ! is_wine_python_package_installed "MetaTrader5==$metatrader_version"; then
    $wine_executable python -m pip install --no-cache-dir MetaTrader5==$metatrader_version
fi

show_message "[8] Installing fastapi, uvicorn library in Windows"
if ! is_wine_python_package_installed "fastapi" or ! is_wine_python_package_installed "pandas" ; then
    $wine_executable python -m pip install --no-cache-dir fastapi uvicorn
fi

show_message "[9] Installing pandas library in Windows"
if ! is_wine_python_package_installed "pandas" ; then
    $wine_executable python -m pip install --no-cache-dir pandas --only-binary :all:
fi

show_message "[10] Checking and installing pyxdg library in Linux if necessary"
if ! is_python_package_installed "pyxdg"; then
    pip install --break-system-packages --no-cache-dir pyxdg
fi

show_message "[11] Starting the app.py server..."
cd /metatrader
$wine_executable python -m uvicorn app:app --host 0.0.0.0 --port 8000 --reload &
sleep 5
if ss -tuln | grep ":8000" > /dev/null; then
    show_message "[10] The app.py server is running on port 8000."
else
    show_message "[10] Failed to start the app.py server on port 8000."
fi
show_message "All setup steps completed. MetaTrader 5 should be running"
