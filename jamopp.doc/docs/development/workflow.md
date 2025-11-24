# Workflow

For the development of the extended JaMoPP, we base the workflow on the Gitflow (cf., [the original](https://nvie.com/posts/a-successful-git-branching-model/) or [Atlassian blog post](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) for more information).

1. While the `main` branch hosts the source code of all releases with Git tags, all changes are primarily merged into the (default) `develop` branch.
1. For every bug, feature, release, and other issue, a [GitHub issue](https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/issues) needs to be created.
1. For the resolution of an issue (except releases), there should be a separate feature branch, usually branched from the current `develop` branch, with the name `<issue_number>_<meaningful_short_issue_description_with_words_separated_by_underscores>`. Please consider the `CHANGELOG.md` and this documentation, and update them if necessary.
1. If the changes are ready for merging, a pull request needs to be created for meging the feature branch into the `develop` branch. It is reviewed by at least one person. After all comments and issues within the pull request are resolved and the pull request is approved, it is merged, closing the pull request and corresponding GitHub issue.
1. For a release:
   1. The following parts should be adapted on the `develop` branch:
      * The version number (at least removing the SNAPSHOT part).
      * The `CHANGELOG.md`.
      * The documentation.
   1. Then, merge `develop` into the `main` branch, and create a tag for the new release.
   1. Run the `release` GitHub action workflow to build and push the artifacts to the MDSD tools update site and Maven central.
   1. Update the version number on the `develop` branch once again (select an appropriate higher version number and re-add the SNAPSHOT part).
