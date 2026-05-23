FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=America/Sao_Paulo

# Configurar o timezone de forma definitiva na imagem
RUN apt-get update && apt-get install -y tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && dpkg-reconfigure --frontend noninteractive tzdata \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# 1. Instalar dependências básicas, WineHQ atualizado e Xvfb
RUN dpkg --add-architecture i386 \
    && apt-get -q update \
    && apt-get -q upgrade -y \
    && apt-get -q install -y \
    software-properties-common \
    wget \
    curl \
    xvfb \
    gnupg2 \
    scrot\
    ca-certificates \
    libegl1:i386\
    libegl1\
    libgnutls30 \
    && mkdir -pm755 /etc/apt/keyrings \
    && wget -O - https://dl.winehq.org/wine-builds/winehq.key | gpg --dearmor -o /etc/apt/keyrings/winehq-archive.key -\
    && wget -NP /etc/apt/sources.list.d/ https://dl.winehq.org/wine-builds/ubuntu/dists/noble/winehq-noble.sources\
    && apt-get  -q update \
    && apt-get install -y --install-recommends winehq-staging \
    # Atualiza as chaves de segurança do sistema
    && update-ca-certificates \    
    && apt-get  -q clean 

# 2. Configurar o ambiente Wine
ENV WINEPREFIX=/root/.wine
ENV WINEARCH=win64
RUN winecfg

# 3. Descarregar o instalador do MT5 (ou copie o seu terminal já instalado)
# Dica: É mais fácil instalar o MT5 numa máquina Windows e copiar a pasta "MetaTrader 5" do Program Files para o container
RUN mkdir -p /root/.wine/drive_c/mt5
# Se preferir automatizar o download do instalador oficial:
# RUN wget https://download.mql5.com/cdn/web/metaquotes.software.corp/mt5/mt5setup.exe -O /tmp/mt5setup.exe

COPY ./MetaTrader5/. /root/.wine/drive_c/mt5/
COPY ./config.ini /root/.wine/drive_c/mt5

# 4. Criar o script de inicialização do monitor virtual + MT5
RUN echo '#!/bin/bash\n\
# Inicia video virtual em background (Display :1)\n\
Xvfb :1 -screen 0 1024x768x16 &\n\
export DISPLAY=:1\n\
\n\
echo "Atualizando prefixo do Wine e certificados..."\n\
export SSL_CERT_FILE=/etc/ssl/certs/ca-certificates.crt\n\
LIBGL_DRI3_DISABLE=1 wineboot --update\n\
\n\
echo "A iniciar MetaTrader 5..."\n\
# Executa o MT5 via Wine\n\
LIBGL_DRI3_DISABLE=1 wine "C:\\mt5\\terminal64.exe" /portable "/config:C:\\mt5\\config.ini" \n\
' > /entrypoint.sh && chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
#ENTRYPOINT ["bash"]