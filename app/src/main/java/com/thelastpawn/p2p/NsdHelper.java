package com.thelastpawn.p2p;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;

/**
 * Network Service Registry and Discovery
 * <p>
 * Constructor: arguments -> Context, ServiceName
 * </p>
 */
class NsdHelper {

    private static final String TAG = "NsdHelper";
    private static final String SERVICE_NAME = "NsdService";
    private static final String SERVICE_TYPE = "_http._tcp.";


    private boolean discovering = false;
    private boolean registered = false;

    private final Context mContext;
    private NsdServiceInfo mServiceInfo = null;
    private NsdManager mNsdManager;
    private MyRegistrationListener myRegistrationListener;
    private MyDiscoveryListener myDiscoveryListener;
    private MyResolveListener myResolveListener;
    private String mServiceName;

    NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

    }

    void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, myRegistrationListener);

        mServiceInfo = serviceInfo;
    }

    void initListeners() {
        initRegistrationListener();
        initDiscoveryListener();
        initResolveListener();
    }

    /**
     * Returns the info of resolved service.
     * Call only after resolving a service to get non-null value.
     * i.e returns null if service is not resolved.
     * @return Discovered and Resolved Service.
     */
    NsdServiceInfo getChosenServiceInfo() {
        return mServiceInfo;
    }

    /**
     * Start discovering nearby registered hosts.
     */
    void discoverServices() {
        if (!isDiscovering() && mNsdManager != null) {
            Log.i(TAG, "Starting Discovery");
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, myDiscoveryListener);
            discovering = true;
        }
    }

    /**
     * Ensure that device is not in state of discovering new hosts.
     */
    void stopDiscovery() {
        if (isDiscovering()) {
            Log.i(TAG, "Stopping Discovery");
            mNsdManager.stopServiceDiscovery(myDiscoveryListener);
            discovering = false;
        }
    }

    /**
     * @return state if the device is trying to discover new hosts/services.
     */
    boolean isDiscovering() {
        return discovering;
    }

    /**
     * closes Registered service
     */
    public void tearDown() {
        if (registered) {
            mNsdManager.unregisterService(myRegistrationListener);
            registered = false;
        }
    }

    /**
     * @return state if service is registered from localhost
     */
    boolean isRegistered() {
        return registered;
    }


    private void initResolveListener() {
        myResolveListener = new MyResolveListener();
    }

    private void initRegistrationListener() {
        myRegistrationListener = new MyRegistrationListener();
    }

    private void initDiscoveryListener() {
        myDiscoveryListener = new MyDiscoveryListener();
    }

    /**
     * Listens only when trying to we register the service.
     * On success, Logs the Service name, port and host IP
     */
    private class MyRegistrationListener implements NsdManager.RegistrationListener {
        @Override
        public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.e(TAG, "Registration Failed");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.e(TAG, "unregistration Failed");
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, "Registration Success");
            mServiceInfo = nsdServiceInfo;
            mServiceName = nsdServiceInfo.getServiceName();
            registered = true;
            Log.i(TAG, "Service Name: " + nsdServiceInfo.getServiceName()
                    + ", port: " + nsdServiceInfo.getPort()
                    + ", Ip: " + nsdServiceInfo.getHost().getHostAddress());
//  Toast.makeText(mContext, "Registered : " + nsdServiceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
            registered = false;
            Log.i(TAG, "Unregistered");
        }
    }

    /**
     * Discovers nearby hosts.
     * Calls resolve listener when Discovery id successful.
     */
    private class MyDiscoveryListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String s, int i) {
        }

        @Override
        public void onStopDiscoveryFailed(String s, int i) {

        }

        @Override
        public void onDiscoveryStarted(String s) {
            Toast.makeText(mContext, "Discovery Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDiscoveryStopped(String s) {
            Toast.makeText(mContext, "Discovery Stopped", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, "Service Discovered " + nsdServiceInfo.getServiceName());
            Toast.makeText(mContext, "Discovery Found " + nsdServiceInfo.getServiceName(), Toast.LENGTH_SHORT).show();

            if (!nsdServiceInfo.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
//            } else if (nsdServiceInfo.getServiceName().equals(mServiceName)) {
//                // The name of the service tells the user what they'd be
//                // connecting to. It could be "Bob's Chat App".
//                Log.d(TAG, "Same machine: " + mServiceName);
//                myResolveListener = new MyResolveListener();
//                mNsdManager.resolveService(nsdServiceInfo, myResolveListener);
            } else if (nsdServiceInfo.getServiceName().contains(SERVICE_NAME)) {
                myResolveListener = new MyResolveListener();
                mNsdManager.resolveService(nsdServiceInfo, myResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo nsdServiceInfo) {

        }
    }

    /**
     * Resolves/Connects with Discovered Service
     */
    private class MyResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.i(TAG, "Service Resolution Failed");
        }

        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, "Service Resolved");
            mServiceInfo = nsdServiceInfo;
            Log.i(TAG, "Service Name: " + nsdServiceInfo.getServiceName()
                    + ", port: " + nsdServiceInfo.getPort()
                    + ", Ip: " + nsdServiceInfo.getHost().getHostAddress());

//            if (nsdServiceInfo.getServiceName().equals(mServiceName)) {
//                Log.d(TAG, "Same IP.");
//                Toast.makeText(mContext, "LocalHost : " + nsdServiceInfo.getPort(), Toast.LENGTH_SHORT).show();
//                return;
//            }
//            int port = mServiceInfo.getPort();
//            InetAddress host = mServiceInfo.getHost();
//            Toast.makeText(mContext, "Service Resolved : " + nsdServiceInfo.getHost() + " : " + nsdServiceInfo.getPort(), Toast.LENGTH_SHORT).show();
        }
    }
}