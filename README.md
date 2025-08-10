![logo](https://github.com/lynxjr3/Catfish/blob/main/logo.png)

[Visit the site!](https://sites.google.com/view/thecatco/projects)
# Catfish Browser Project

Catfish Browser is a lightweight, open-source web browser built on Chromium. It aims to provide a fast and minimal browsing experience with low resource usage.

**Why use Catfish?**

-   Lightweight and fast
-   Open-source and community-driven
-   Simple and easy to use

![logo](https://github.com/lynxjr3/Catfish/blob/main/app.png)

**Get involved!**  
If you encounter any issues or want to contribute new features, feel free to open an issue or submit a pull request. Your help is always appreciated!

> **Warning:** This project is currently in beta and is not recommended for daily or primary use.

> **Note:** There is only release for Windows right now. In version 0.4 Linux version would be avalible.

> **Known Bugs:** Downloading file causes the browser to crash.

## How to install

To install go to [releases](https://github.com/lynxjr3/Catfish/releases) and download the installer file and open it and follow the steps. After that it should work!

#
## How to Contribute and Set Up the Development Environment

If you want to contribute, modify the project, or make it your own, I recommend using [**NetBeans 26**](https://netbeans.apache.org/front/main/index.html) as the IDE.
You will need to download [**JCEF** (Java Chromium Embedded Framework)](https://github.com/jcefmaven/jcefbuild/releases/tag/1.0.66)
After downloading, extract the JCEF files into a folder you create (make sure to extract **only the `bin` folder**). Then, add the following VM option to your project configuration:

`-Djava.library.path=..\..\bin\lib\win64` 

Finally, open the project in NetBeans, and you’re ready to start developing!
