val versionPropsFile = file("version.properties")

if (!versionPropsFile.exists())
    versionPropsFile.createNewFile()

fun autoIncrementBuildNumber() {
    if (versionPropsFile.canRead()) {
        val versionProps = java.util.Properties()
        versionProps.load(versionPropsFile.inputStream())
        extra["buildNumber"] = versionProps.getProperty("VERSION_BUILD", "0").toInt() + 1
        versionProps["VERSION_BUILD"] = extra["buildNumber"].toString()
        versionProps.store(
            versionPropsFile.outputStream(),
            "Automatic Build Number. DO NOT ALTER!!!"
        )
    }
}

if (versionPropsFile.canRead()) {
    val versionProps = java.util.Properties()
    versionProps.load(versionPropsFile.inputStream())
    extra["buildNumber"] = versionProps.getProperty("VERSION_BUILD", "0").toInt() + 1
}

gradle.taskGraph.whenReady {
    if (this.hasTask(tasks.getByName("assembleRelease")))
        autoIncrementBuildNumber()
}
