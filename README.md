Neat Youtube integration
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) for the [Magnolia CMS](http://www.magnolia-cms.com) automatically obtaining metadata about any referenced videos from youtube

Module will register new rest client service called ```youtube``` and will use that service to obtain metadata about any video you insert in a dialog.

In order to trigger obtaining of the metadata for the field with id of the video, you need to configure ```commit``` action of the dialog containing field with the video to have ```class=com.neatresults.mgnltweaks.neatu2b.ui.action.AddU2BMetadataSaveDialogAction$Definition```

By default, commit action of the dialog would be looking at value of field called ```youtube```. If you happen to name your field for storing youtube video ids or links differently, add property named ```idFieldName``` and set its value to the name of your youtube video field.

Once you have configured the dialog you can start using it. Module will be able to process id from the field whenever you use it to store id or the link to youtube video containing the id.

Upon saving the dialog, action will use Magnolia's rest-client to execute 2 calls against Youtube API. First it will make ```part=snippet``` call to obtain description, thumbnails and time of publishing. Then it will make ```part=contentDetails``` call to obtain duration and dimension of the video. Each of the calls will cost 2 credits against your API key which is the reason why action saves the results from the calls to avoid having to execute them every time video details are quested.

Tip: For security reasons you should keep your api key configured only on author instance. There is no reason to have it on public since you are not doing any editing there.

Upon saving action will store all values using ```<fieldName><propertyName>``` naming pattern at the same level where the other fields from the dialog are stored.

The properties stored are:

- all thumbnails (typically default, standard, medium, high, maxres)
- description
- title
- publishedAt
- duration
- definition

resulting structure would be (assuming your field name is ```youtube```)
```
myComponent
|- youtubeTitle=
|- youtubeDescription=
|- youtubeDefinition=
|- youtubeDuration=
'- youtubeThumbs
   |- high
   |  |- url=
   |  |- width=
   |  '- height=
   |- medium
   |  |- url=
   |  |- width=
   |  '- height=
   |- standard
   |  |- url=
   |  |- width=
   |  '- height=
   |- default
   |  |- url=
   |  |- width=
   |  '- height=
   '- maxres
      |- url=
      |- width=
      '- height=
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
      <version>1.0</version>
    </dependency>
```

Versions
-----------------
Version 1.0 should be compatible with all Magnolia 5.3 or higher versions, but was tested only on 5.3.15 and not before or after. If you run into any issues w/ other versions, please report them back.

Latest version can be found at https://nexus.magnolia-cms.com/service/local/repositories/magnolia.forge.releases/content/com/neatresults/mgnltweaks/neat-u2b/1.0/neat-u2b-1.0.jar

Installation & updates 
-----------------
Upon instalation with samples, module will register extra field in stk ```tabMetadata``` dialog tab and configure action in stk home template so you can test the youtube field in page properties dialog of home. If you don't want that to happen, you want to install module with ```magnolia.bootstrap.samples=false``` in your ```magnolia.properties```.

Changes
-----------------

1.0
- first release
