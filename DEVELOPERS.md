# Developers

## Building

Follow these steps to build the openEQUELLA BIRT plugins.

1. Download the BIRT "All-in-One" package from https://download.eclipse.org/birt/downloads/ and unzip it into a permanent directory, i.e. this is "installing" it. Don't get the Framework or Runtime downloads; the reason being is that the version of Eclipse you develop with is likely to be a different version to the BIRT downloads (the latest build of BIRT is from 2018, using Eclipse Neon) and you don't want to rely on your development Eclipse's plugins to fill the voids of the BIRT Framework. Besides, it will also be beneficial to actually be able to design and run reports in the "All-in-One" report designer.

2. Download and install the "Eclipse IDE for RCP and RAP Developers" from https://www.eclipse.org/downloads/packages/. It probably doesn't matter much which Eclipse package you download. E.g. "Eclipse IDE for Enterprise Java Developers" is probably another good, and more general purpose, choice. Any missing functionality can be added to Elipse via "Help->Install New Software..." menu.

3. Launch "Eclipse IDE for RCP and RAP Developers" and create a workspace. (Wherever you like. It seems appropriate to create the workspace in the openEQUELLA-reporting-plugin directory. Note that the workspace .metadata directory is already included in the .gitignore file)

4. In the Project Explorer window, select "Import projects..." -> "General" -> "Existing Projects into Workspace". Browse to the root of openEQUELLA-reporting-plugin repository and select that folder. Make sure all the discovered projects are selected and then click Finish.

5. At this point, you will be seeing compilation errors (missing imports), so we need to add a "Target Platform". Go to Window->Preferences->Plug-in Development->Target Platform. You will note that it currently says "Running Platform (Active)". This means you are using the current development Eclipse's plugins as dependencies, which doesn't include the BIRT plugins.

6. From the screen in step 5, click the "Add..." button, choose "Nothing" as the base, and click "Next". Name the Target Platform "BIRT" (or "Mr. T", whatever, it doesn't matter) and click "Add..." on the Locations tab.

7. From the "Add Content" dialog that comes up, choose "Installation" and browse to the root of the BIRT folder (the folder that includes the eclipse.ini and executable). Click "Finish", but remain in the "New Target Definition" dialog.

8. If we were to finish creating the Target Platform now, our plugins would mostly build, but would still be missing XStream dependencies. From the "New Target Definition" dialog, click "Add..." again, but this time choose "Software Site". Click "Add..." and an "Add Repository" dialog comes up. Enter "Orbit"\* in the Name field, and paste https://download.eclipse.org/tools/orbit/downloads/drops/R20200224183213/repository into the Location field. Once you click "Add", wait for the list of plugins to download and display.

9. In the "type filter text" box, enter "xstream", and tick the box next to XStream that displays in the tree below. Now click "Finish".

10. From the "Edit Target Definition" dialog, click "Finish" again. Now click the checkbox against your newly added Target definition. Click "Apply and Close". After a short build time, your compilation errors should disappear.

11. Right click on the "openEQUELLA Reporting Plugins" project and choose "Export..."->"Plug-in Development"->"Deployable features". Select a destination directory and click Finish.

12. The contents of the "plugins" sub-directory of the export directory in the previous step are deployable to BIRT. Copy all of these jar files into the "dropins" directory of you BIRT designer installation.

13. Start the BIRT designer and follow the section at the top of this document titled "Report Designers".

\* - _Orbit is kind-of the equivalent of a Maven Central for Eclipse plugins, however they have a strict policy on what gets into the repository. Luckily for us they have an XStream plugin (this is actually just a plain XStream jar that has been modified to contain OSGI plugin information). If we needed a dependency that didn't already exist as an OSGI plugin, you'd probably have to manually create one and commit it into the openEQUELLA-reporting-plugin repository. (But there may be a better way)_

## Deployment

### Signing

#### Generating a new key:

1. `gpg --gen-key` I used (openEQUELLA / openEQUELLA@apereo.org / mysecretpassphrase)
2. `gpg --list-keys` (Copy the long ID)
3. `gpg --keyserver http://keys.openpgp.org --send-keys [the long ID]`

#### Using the key I already made:

1. Probably not necessary...

For more info, read this:
https://central.sonatype.org/pages/working-with-pgp-signatures.html

### Maven

Edit your Maven settings.xml file (You will need an OSSRH account: sign up to https://issues.sonatype.org, and then ask Aaron or Ian to request access for you)

```xml
<settings>
  ...
  <servers>
  ...
     <server>
        <id>ossrh</id>
        <username>your-jira-id</username>
        <password>your-jira-pwd</password>
     </server>
  </servers>
  ...
</settings>
```

For more info, read this:
https://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html

#### Plugins

Note that before deploying each plugin binary, you:

1. Copy the generated com.tle.reporting.\* plugins into the corresponding directory in [repo root]/deployment/plugins and rename to bin.jar
2. Update the version number in the pom.xml file in each of these directories.
3. Create a sources.jar (just zipped up raw source of the plugin)
4. Run the `deploy.bat` (or `deploy.sh`) script in the [repo root]/deployment/plugins directory (Make sure you have Maven installed and have the mvn binary on your PATH.)
   On Windows, this will spawn 3 new command windows, so pay attention to whether they all succeed or not.
   I haven't tested \*nix script, so help here would be appreciated.

#### Birt Framework

This will need to be done if you want to upgrade the version of the BIRT binaries used by the openEQUELLA server.

1. Zip up the contents on the plugins directory of the latest BIRT framework. Rename the zip file to birt.zip and copy into [repo root]/deployment/birt-framework
2. Update the pom.xml in [repo root]/deployment/birt-framework
3. Run the `deploy.bat` (or `deploy.sh`) in that directory. Note that this might take a little while due to the size of the zip.

### Releasing

#### Plugins

1. Login to https://oss.sonatype.org/ (you should already have an account you entered into your Maven settings.xml file)
2. Click on Staging Repositories on the left.
3. Click on a staging repository (Check the Contents tab on the lower panel to see if it has everything you just deployed).
4. Click on Close, and then wait a while before clicking refresh. If the gods are in your favour, and the moon is in right phase, then it will close for you. Otherwise you need to address the errors listed in the Activity tab.
5. Click on Release
6. WINNING

## Including in openEQUELLA

1. Update the version numbers in `build.sbt` file at [openEQUELLA rep]/Source/Plugins/Birt/org.eclipse.birt.osgi
2. Build openEQUELLA as you normally would, noting that there will be some delay between releasing your binaries on OSS and it becoming available on Maven Central. You may need to wait up to a few hours before the build process can find your binaries.
