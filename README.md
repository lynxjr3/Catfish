![logo](https://github.com/lynxjr3/Catfish/blob/main/logo.png)

[Visit the site!](https://sites.google.com/view/thecatco/projects)
# Catfish Browser Project

Catfish Browser is a lightweight, open-source web browser built on Chromium. It aims to provide a fast and minimal browsing experience with low resource usage.

**Why use Catfish?**

-   Lightweight and fast
-   Open-source and community-driven
-   Simple and easy to use

![Catfish Browser Screenshot](https://github.com/lynxjr3/Catfish/blob/main/app.png)

**Get involved!**  
If you encounter any issues or want to contribute new features, feel free to open an issue or submit a pull request. Your help is always appreciated!

> **Warning:** This project is currently in beta and is not recommended for daily or primary use.

> **Note:** Currently available for **Windows only**.  
> Linux support is planned for version 0.4.

## Known Bugs
- Downloading a file causes the browser to crash (will be fixed in a future release).

## How to Install

To install, go to [releases](https://github.com/lynxjr3/Catfish/releases) and download the installer file.  
Open it and follow the steps. After that, Catfish should be ready to use!

#
## How to Contribute and Set Up the Development Environment

If you want to contribute, modify the project, or make it your own, I recommend using [NetBeans 26](https://netbeans.apache.org/front/main/index.html) as the IDE.
1. [Download JCEF (Java Chromium Embedded Framework)](https://github.com/jcefmaven/jcefbuild/releases/tag/1.0.66).
2. Extract the JCEF files into a folder you create. Only extract the bin folder.
3. In NetBeans, go to your project’s VM options and add:

`-Djava.library.path=(your path)\bin\lib\win64`

(Replace (your path) with the full folder path on your system.)

Open the project in NetBeans, and you’re ready to start developing!
