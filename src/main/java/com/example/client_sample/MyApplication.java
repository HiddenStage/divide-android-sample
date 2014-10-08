/*
 * Copyright (C) 2014 Divide.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.client_sample;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;
import com.jug6ernaut.android.logging.ALogger;
import com.jug6ernaut.android.logging.Logger;
import io.divide.client.Backend;
import io.divide.client.android.AndroidBackend;
import io.divide.client.android.mock.AndroidDebugConfig;
import io.divide.client.android.push.PushEvent;
import io.divide.client.android.push.PushListener;

public class MyApplication extends Application {

    private static Logger logger;
    private Handler handler = new Handler();

    @Override
    public void onCreate(){
        ALogger.init(this, "Backend", true);
        logger = Logger.getLogger(MyApplication.class);

        AndroidBackend b = Backend.init(new AndroidDebugConfig(this,getProdUrl(),getDevUrl()));
//        BackendServices.addPushListener(listener);
    }

    @Override
    public void onTerminate() {

    }

    private String getProdUrl(){
        return getString(R.string.prodUrl);
    }

    private String getDevUrl(){
        return getString(R.string.devUrl);
    }

    private PushListener listener = new PushListener() {

        @Override
        public void onEvent(final PushEvent event) {
            logger.debug("Push Message: " + event);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.this, "Push Message: " + event, Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
