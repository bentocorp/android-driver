package com.bentonow.drive.listener;

import org.bentocorp.api.ws.Push;

/**
 * Created by Jose Torres on 11/10/15.
 */
public interface NodeEventsListener {
    void onPush(Push mPush);
}
