# Apache Nutch 2.3.* - Advanced URL Normalizer - Plugin
A plugin developed for Apache Nutch 2.3.* to further normalize honour URLs to reducing duplicate entries at index. Apache Nutch 2.3.* URL normalizer plugins do NOT lower the case on the URL paths and query parameters. This means that there's chance nutch will index duplicate entries if it crawls the same URL present in different case. For example:
``http://mydomain.com/hello?name=2`` 
``http://mydomain.com/hElLo?name=2`` 
``http://mydomain.com/hElLo?nAMe=2`` 
All 3 of the above pages will indexed to your external datastore because of a difference in case.

The URL Normalizer Advanced plugin converts the URL ``path`` and the ``key`` to lowercase reducing the chances of duplicate entries getting indexed to your external data store.

##Usage


##How To Build

Before proceeding make sure you have ``apache ant`` installed. 

In order to use this plugin you'll need to build it into Apache Nutch 2.3.*

Follow these steps to ensure your plugin gets built into the runtime folder:

1. Copy the plugin files to the ``NUTCH_ROOT/src/plugin`` folder. 

2. Open ``NUTCH_ROOT/src/plugin/build.xml`` and add the following children to the associated target elements:  
    ```
        <target name="deploy">
            <ant dir="metarobots" target="deploy"/>
        </target>
        
        <target name="test">
            <ant dir="metarobots" target="test"/>
        </target>
        
        <target name="clean">
            <ant dir="metarobots" target="clean"/>
        </target>
    ```

    **Do NOT overwrite any existing ```<ant>``` elements**
    
3. Open a terminal and navigate to the ``NUTCH_ROOT`` directory.

   Enter the following command:
   
   ```ant runtime```
   




##Running Tests
To test the plugin, open a terminal and navigate to the ``NUTCH_ROOT/runtime/local`` directory. 

Run the following command:

```
 sudo bin/nutch indexchecker http://domain.com/Nutch-UrlNormalizer-Advanced?PlugIN=1
```
The result output of the URL should be ``http://domain.com/nutch-urlnormalizer-advanced?plugin=1``.
