plugins {
    id("de.fntsoftware.gradle.markdown-to-pdf")
}

markdownToPdf {
    cssFile = "$projectDir/style.css"
}

val mdToPdfTask = tasks.create("createPdf", de.fntsoftware.gradle.MarkdownToPdfTask::class.java) {
    setInputFile("$projectDir/README.md")
    setOutputFile("$projectDir/README.pdf")
}

val mdToHtmlTask = tasks.create("createHtml", de.fntsoftware.gradle.MarkdownToHtmlTask::class.java) {
    setInputFile("$projectDir/README.md")
    setOutputFile("$projectDir/README.html")
}

tasks.create("build") {
    group = "build"
    dependsOn(mdToPdfTask)
    dependsOn(mdToHtmlTask)
}
