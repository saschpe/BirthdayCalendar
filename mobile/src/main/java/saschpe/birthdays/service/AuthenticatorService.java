package saschpe.birthdays.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import saschpe.birthdays.accounts.BirthdaysAccountAuthenticator;

/**
 * A bound Service that instantiates the account_authenticator when started.
 */
public class AuthenticatorService extends Service {
    private BirthdaysAccountAuthenticator birthdaysAccountAuthenticator;

    @Override
    public void onCreate() {
        birthdaysAccountAuthenticator = new BirthdaysAccountAuthenticator(this);
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link android.os.IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return birthdaysAccountAuthenticator.getIBinder();
    }
}
