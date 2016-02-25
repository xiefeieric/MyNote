package uk.co.feixie.mynote.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Fei on 18/02/2016.
 */
public class ServerApps implements Serializable {

    public ArrayList<App> apps;

    @Override
    public String toString() {
        return "ServerApps{" +
                "apps=" + apps +
                '}';
    }

    public class App implements Serializable {
        public String id;
        public String title;
        public String status;
        public String icon;
        public String desc;
        public String uri;
        public String packageName;

        @Override
        public String toString() {
            return "App{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", status='" + status + '\'' +
                    ", icon='" + icon + '\'' +
                    ", desc='" + desc + '\'' +
                    ", uri='" + uri + '\'' +
                    ", packageName='" + packageName + '\'' +
                    '}';
        }
    }

}
