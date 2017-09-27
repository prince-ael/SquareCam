package com.ael_mnisiddique.com.squarecam;

/**
 * Created by adaptive on 9/18/17.
 */

public interface ActivityListener {

    void onCaptureButtonClicked(String directoryPath);
    void startCamera();
    void stopCamera();
}
