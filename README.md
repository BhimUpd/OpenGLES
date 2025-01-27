NOTE : This is just Activity and xml ppage code and connected Java files

To use thes please add 

        <activity
            android:name=".YOUR_ACTIVITY_NAME"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

to your AndroidManifest.xml code and make sure do       android:exported="false"         to whatever the activity you were using before.
