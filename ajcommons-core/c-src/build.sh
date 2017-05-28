#!/bin/sh

gcc -arch x86_64 -arch i386 -bundle -I/System/Library/Frameworks/JavaVM.framework/Headers -o libAuthKit.jnilib -framework JavaVM -framework Security *.c