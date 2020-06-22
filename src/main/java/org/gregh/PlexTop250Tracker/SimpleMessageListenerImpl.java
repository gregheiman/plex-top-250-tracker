package org.gregh.PlexTop250Tracker;

import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.InputStream;

public class SimpleMessageListenerImpl implements SimpleMessageListener {
    public boolean accept(String from, String recipient) {
        System.out.println("accept: ${from} \n>> ${recipient}");
        return true;
    }

    public void deliver(String from, String recipient, InputStream data) {
        System.out.println("deliver: ${from} \n>> ${recipient} \n>>> ${data.read()}");
    }
}
