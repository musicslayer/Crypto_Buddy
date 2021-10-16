package com.musicslayer.cryptobuddy.crash;

// An exception that will cause Crash classes to do nothing, instead of showing the CrashReporterDialog.
// Note that Crash class methods that are supposed to return something will not respect this bypass.
public class CrashBypassException extends RuntimeException {}
