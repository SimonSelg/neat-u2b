Neat Youtube integration
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) for the [Magnolia CMS](http://www.magnolia-cms.com) automatically obtaining metadata about any referenced videos from youtube

Module will register new rest client service called ```youtube``` and will use that service to obtain metadata about any video you insert in a dialog.

In order to trigger obtaining of the metadata for the field with id of the video, you need to add field ```com.neatresults.mgnltweaks.neatu2b.ui.form.field.U2BField$Definition``` to your dialog or if all you want is youtube player, add directly ```neat-u2b:components/youtube``` component to your page template list of available components.

Module contains also alternative implementations of action or composite field, but those are not meant for production use. More details at https://www.magnolia-cms.com/blogs/jan-haderka/detail~@complex-dialog-fields~.html

Upon clicking 'Fetch Metadata' button in the dialog, Magnolia's rest-client will be called to execute 2 calls against Youtube API. First it will make ```part=snippet``` call to obtain description, thumbnails and time of publishing. Then it will make ```part=contentDetails``` call to obtain duration and dimension of the video. Each of the calls will cost 2 credits against your API key which is the reason why action saves the results from the calls to avoid having to execute them every time video details are requested.

Tip: For security reasons you should keep your api key configured only on author instance. There is no reason to have it on public since you are not doing any editing there.

Upon saving action will store all values under subnode in the component named after property name.

The properties stored are:

- all thumbnails (default, standard, medium, high, maxres)
- description
- title
- publishedAt
- duration
- definition

resulting structure would be (assuming your field name is ```youtube```)
```
myComponent
|- youtube
   |- title=
   |- description=
   |- definition=
   |- duration=
   |- published=
   |- highUrl=
   |- highWidth=
   |- highHeight=
   |- mediumUrl=
   |- mediumWidth=
   |- mediumHeight=
   |- standardUrl=
   |- standardWidth=
   |- standardHeight=
   |- defaultUrl=
   |- defaultWidth=
   |- defaultHeight=
   |- maxresUrl=
   |- maxresWidth=
   '- maxresHeight=
```

License
-------

Released under the GPLv3, see LICENSE.txt. 

Feel free to use this app, but if you modify the source code please fork us on Github.

Maven dependency
-----------------
```xml
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-u2b</artifactId>
      <version>2.0</version>
    </dependency>
```

Versions
-----------------
Version 1.0 should be compatible with all Magnolia 5.3 or higher versions, but was tested only on 5.3.15 and not before or after. If you run into any issues w/ other versions, please report them back.
Version 2.0 should be compatible with 5.3 and 5.4, but was tested only on 5.4.7 and not before. If you run into any issues w/ other versions, please report them back.

Latest version can be found at https://nexus.magnolia-cms.com/service/local/repositories/magnolia.forge.releases/content/com/neatresults/mgnltweaks/neat-u2b/2.0/neat-u2b-2.0.jar

Installation & updates 
-----------------
Upon instalation with samples, module will register extra field in stk ```tabMetadata``` dialog tab and configure action in stk home template so you can test the youtube field in page properties dialog of home. If you don't want that to happen, you want to install module with ```magnolia.bootstrap.samples=false``` in your ```magnolia.properties```.

Changes
-----------------

2.0
- added field for saving Youtube metadata
- added youtube player component for use out of the box
- simplified storage structure
- fetched data can now be edited and/or refetched independently from saving the dialog

1.0
- first release
- added action for saving Youtube metadata
 

