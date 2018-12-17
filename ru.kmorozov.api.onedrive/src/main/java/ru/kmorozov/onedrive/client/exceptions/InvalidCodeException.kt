package ru.kmorozov.onedrive.client.exceptions

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
class InvalidCodeException(errorInfo: OneDriveErrorInfo) : OneDriveException(errorInfo)
