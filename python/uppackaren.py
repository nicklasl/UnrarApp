#!/usr/bin/python

import fnmatch
import os
import pydash
from unrar import rarfile


def is_rar_archive(path):
    try:
        files = os.walk(path).next()[2]
        for file in files:
            if fnmatch.fnmatch(file, "*.rar"):
                return True
        return False
    except StopIteration:
        return False


def has_sub_folders(path):
    try:
        sub_folders = os.walk(path).next()[1]
        return sub_folders
    except StopIteration:
        return False


def unpack(dir):

    pass


###########

initialPath = "/Users/nicklas/Downloads/Mappen"
dir = os.listdir(initialPath)


for file in dir:
    fullPath = initialPath + "/" + file
    if is_rar_archive(fullPath):
        print file + " is a rar archive"
        unpack(fullPath)
    if has_sub_folders(fullPath):
        print file +" has sub folders"


#rar = rarfile.RarFile('sample.rar')
#rar.namelist()
