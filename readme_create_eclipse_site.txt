steps to create a new eclipsed site with the plugins in a new version:
1. edit ow\src\eclipse\net.vtst.eclipse.easyxtext\META-INF\MANIFEST.MF:
		set a new Bundle-Version, e.g. Version: 1.0.15.msg-life-1
2. edit ow\src\eclipse\net.vtst.eclipse.easyxtext.ui\META-INF\MANIFEST.MF:
		set a new Bundle-Version, e.g. Version: 1.0.15.msg-life-1
3. edit ow\src\eclipse\net.vtst.ow.eclipse.less\META-INF\MANIFEST.MF:
		set a new Bundle-Version, e.g. Version: 1.0.22.msg-life-1
3. edit ow\src\eclipse\net.vtst.ow.eclipse.less.ui\META-INF\MANIFEST.MF:
		set a new Bundle-Version, e.g. Version: 1.0.22.msg-life-1
4. open ow\src\eclipse\net.vtst.ow.eclipse.less.feature\feature.xml in Eclipse
		- click tab "included plugins"
		- click button "Versions..."
		- Select option "Copy versions from plug-in and fragment manifests" and click "Finish"
		- check that the list of included plug-ins contains entries net.vtst.elipse.easytext, net.vtst.elipse.easytext.ui,
		  net.vtst.ow.eclipse.less and net.vtst.ow.eclipse.less.ui in the versions you have entered into the MANIFEST.MFs above
		  (if not:
				- remove the 4 entries
				- click button "Add..."
				- enter "net.vtst" into "Select a plugin" field
				- select the plugins and click "OK")
		- click tab "Overview"
		- update version to something like "1.0.22.msg-life-1"
5. open C:\Projekte\IPL\Eclipse-LESS-Plugin\movalz_fork\ow\src\eclipse\net.vtst.ow.eclipse.site\site.xml in Eclipse:
		- open root tree node and remove all existing entries
		- click button "Add Feature..."
		- enter "net.vtst" into "Enter an ID of a feature..." field, select entry and click OK
		- click button "Build All"
6. result can be found in ow\src\eclipse\net.vtst.ow.eclipse.site
		- copy of files of this directory exect .project and .gitignore to a network location like
		  \\le-s-fs02\projekte\OpenIpl\tools\eclipse\less-css-plugin-1.0.22.msg-life-1\net.vtst.ow.eclipse.site
		  and use this location to install plugins into an eclipse installation
