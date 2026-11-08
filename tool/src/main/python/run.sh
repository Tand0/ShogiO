#!/bin/bash

if [ -f ".env/bin/activate" ]; then
    echo   .env/bin/activate
    source .env/bin/activate
else
    echo ".env NOT found. So create..."
    python3 -m venv .env
    echo "install -r requirements.txt"
    pip install -r requirements.txt
fi

echo "Start python code!"
# python3 show_tqdm.py
python3 001_create_mobilenetv5.py

#
