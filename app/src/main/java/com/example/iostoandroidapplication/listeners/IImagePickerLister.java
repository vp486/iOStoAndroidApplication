package com.example.iostoandroidapplication.listeners;

import com.example.iostoandroidapplication.enums.ImagePickerEnum;

@FunctionalInterface
public interface IImagePickerLister {
    void onOptionSelected(ImagePickerEnum imagePickerEnum);
}
