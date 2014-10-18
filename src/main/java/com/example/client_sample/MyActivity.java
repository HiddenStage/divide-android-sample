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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.jug6ernaut.android.logging.Logger;
import io.divide.client.BackendObject;
import io.divide.client.BackendServices;
import io.divide.client.BackendUser;
import io.divide.client.android.AuthActivity;
import io.divide.client.android.mock.DivideDrawer;
import io.divide.client.auth.LoginListener;
import io.divide.shared.transitory.TransientObject;
import io.divide.shared.transitory.query.OPERAND;
import io.divide.shared.transitory.query.Query;
import io.divide.shared.transitory.query.QueryBuilder;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyActivity extends Activity {

    private Logger logger = Logger.getLogger(MyActivity.class);
    private List<BackendObject> objectList = new ArrayList<BackendObject>();
    private BackendObjectAdapter adapter;
    private BackendUser user;

    @InjectView(R.id.cachedUserTV)         TextView savedUserTV;
    @InjectView(R.id.loggedInUserTV)       TextView loggedInUserTV;
    @InjectView(R.id.usersLV)              ListView usersLV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DivideDrawer.attach(this, R.layout.main);
        ButterKnife.inject(this);

        BackendServices.addLoginListener(new LoginListener(){
            @Override
            public void onNext(BackendUser user) {
                System.out.println("loginListener: setUser: " + user);
                if(user != null){
                    setUser(user);
                    getObjects();
                }
            }
        });

        adapter = new BackendObjectAdapter(this,objectList);
        usersLV.setAdapter(adapter);
        usersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BackendObject o = adapter.getItem(position);
                Query q = new QueryBuilder()
                        .delete()
                        .from(BackendObject.class)
                        .where(TransientObject.OBJECT_KEY, OPERAND.EQ, o.getObjectKey())
                        .build();

                BackendServices.remote()
                        .query(BackendObject.class, q)
                        .subscribe(new Action1<Collection<BackendObject>>() {
                            @Override
                            public void call(Collection<BackendObject> backendObjects) {
                                getObjects();
                            }
                        });

                BackendServices.local().delete(o);
            }});

    }

    @Override
    public void onResume(){
        super.onResume();
        setUser(BackendUser.getUser());
    }

    @OnClick(R.id.loginButton)
    public void login(){
        Intent intent = new Intent(MyActivity.this, AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_ENABLE_ANONYMOUS_LOGIN,true);

        MyActivity.this.startActivity(intent);
    }

    @OnClick(R.id.clearSavedUserButton)
    public void logout(){
        BackendUser.logout();
        savedUserTV.setText("Cached User: ");
        loggedInUserTV.setText("User: ");
        objectList.clear();
        adapter.notifyDataSetInvalidated();
    }

    @OnClick(R.id.addObject)
    public void addObject(){
        if(user != null){
            BackendObject object = new BackendObject();
            object.put("score",System.currentTimeMillis()+"");
            object.put("user", user.getUsername());
            BackendServices
                    .remote()
                    .save(object)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void s) {
                            getObjects(); //refresh list
                        }
                    });
        }
    }

    @OnClick(R.id.getObjects)
    public void getObjects(){
        final Query q = new QueryBuilder()
                .select()
                .from(BackendObject.class)
                .limit(10).build();

        BackendServices.remote()
                .query(BackendObject.class, q)
                .subscribe(new Action1<Collection<BackendObject>>() {
                    @Override
                    public void call(Collection<BackendObject> objects) {
                        objectList.clear();
                        objectList.addAll(objects);
                        adapter.notifyDataSetInvalidated();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.debug("", throwable);
                    }
                });
    }

    private void setUser(BackendUser user){
        if(user!=null){
            loggedInUserTV.setText("User: " + user.getEmailAddress());
            this.user = user;
        }
    }

}
