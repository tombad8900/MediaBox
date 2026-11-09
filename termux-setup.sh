#!/bin/bash
echo "MediaBox Termux Setup"
termux-setup-storage
pkg install openjdk-17 git python ffmpeg -y
chmod +x gradlew
echo "Done!"
