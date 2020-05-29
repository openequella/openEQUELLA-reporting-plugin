# openEQUELLA Reporting Plugin

## Report Designers
Details for this project can be found in the openEQUELLA [docs repo](https://equella.github.io/), specifically, the [reporting tutorials](https://github.com/equella/equella.github.io/tree/master/tutorials/reporting).

## Developers
Follow these steps to build the openEQUELLA BIRT plugins.

1. Download the BIRT "All-in-One" package from https://download.eclipse.org/birt/downloads/ and unzip it into a permanent directory, i.e. this "installing" it.  Don't get the Framework or Runtime downloads; the reason being is that the version of Eclipse you develop with is likely to be a different version to the BIRT downloads (the latest build of BIRT is from 2018, using Eclipse Neon) and you don't want to rely on your development Eclipse's plugins to fill the voids of the BIRT Framework.  Besides, it will also be beneficial to actually be able to design and run reports in the "All-in-One" report designer. 

2. Download and install the "Eclipse IDE for RCP and RAP Developers" from https://www.eclipse.org/downloads/packages/.  It probably doesn't matter much which Eclipse package you download. E.g. "Eclipse IDE for Enterprise Java Developers" is probably another good, and more general purpose, choice.  Any missing functionality can be added to Elipse via "Help->Install New Software..." menu.

3. Launch "Eclipse IDE for RCP and RAP Developers" and create a workspace. (Wherever you like. It seems appropriate to create the workspace in the openEQUELLA-reporting-plugin directory. Note that the workspace .metadata directory is already included in the .gitignore file)

4. In the Project Explorer window, select "Import projects..." -> "General" -> "Existing Projects into Workspace".  Browse to the root of openEQUELLA-reporting-plugin repository and select that folder.  Make sure all the discovered projects are selected and then click Finish.

5. At this point, you will be seeing compilation errors (missing imports), so we need to add a "Target Platform".  Go to Window->Preferences->Plug-in Development->Target Platform.  You will note that it currently says "Running Platform (Active)".  This means you are using the current development Eclipse's plugins as dependencies, which doesn't include the BIRT plugins.  

6. From the screen in step 5, click the "Add..." button, choose "Nothing" as the base, and click "Next".  Name the Target Platform "BIRT" (or "Mr. T", whatever, it doesn't matter) and click "Add..." on the Locations tab.

7. From the "Add Content" dialog that comes up, choose "Installation" and browse to the root of the BIRT folder (the folder that includes the eclipse.ini and executable).  Click "Finish", but remain in the "New Target Definition" dialog.  

8. If we were to finish creating the Target Platform now, our plugins would mostly build, but would still be missing XStream dependencies.  From the "New Target Definition" dialog, click "Add..." again, but this time choose "Software Site".  Click "Add..." and an "Add Repository" dialog comes up.  Enter "Orbit"* in the Name field, and paste https://download.eclipse.org/tools/orbit/downloads/drops/R20200224183213/repository into the Location field.  Once you click "Add", wait for the list of plugins to download and display.

9. In the "type filter text" box, enter "xstream", and tick the box next to XStream that displays in the tree below.  Now click "Finish".

10. From the "Edit Target Definition" dialog, click "Finish" again.  Now click the checkbox against your newly added Target definition.  Click "Apply and Close".  After a short build time, your compilation errors should disappear.

11. Right click on the "openEQUELLA Reporting Plugins" project and choose "Export..."->"Plug-in Development"->"Deployable features". Select a destination directory and click Finish.

12. The contents of the "plugins" sub-directory of the export directory in the previous step are deployable to BIRT.  Copy all of these jar files into the "dropins" directory of you BIRT designer installation.

13. Start the BIRT designer and follow the section at the top of this document titled "Report Designers".

* *Orbit is kind-of the equivalent of a Maven Central for Eclipse plugins, however they have a strict policy on what gets into the repository.  Luckily for us they have an XStream plugin (this is actually just a plain XStream jar that has been modified to contain OSGI plugin information).  If we needed a dependency that didn't already exist as an OSGI plugin, you'd probably have to manually create one and commit it into the openEQUELLA-reporting-plugin repository. (But there may be a better way)*

