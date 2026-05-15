#!/bin/bash
# run.sh
export WINEDEBUG=-all
wine "/home/giuseppe/.wine/drive_c/users/giuseppe/AppData/Local/Programs/Python/Python313/python.exe" -u $1
