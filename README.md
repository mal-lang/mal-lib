# MAL lib

A Java library for compiling MAL specifications.

## Release

To make a release, perform the following steps:

1. Make a release commit and push it to a new branch
2. Make a pull request to `master` and get it approved and merged
3. Make a release tag for the merged pull request
4. Make a snapshot commit and get it merged to `master`

### 1. Make a release commit and push it to a new branch

The release commit shall contain the following changes in `pom.xml`:

- `project.version` is set to the release version
- `project.scm.tag` is set to the release tag

The name of the new branch doesn't matter, since it will be deleted after the release commit has been merged to `master`, but the convention for branch names is `<user-name>/<branch-name>`.

The commit message shall be `Release <version>`.

#### Example

In the following example, the current snapshot version is `1.0.0-SNAPSHOT` and the release version is `1.0.0`. The release tag will be `release/1.0.0`, and the next snapshot version will be `1.0.1-SNAPSHOT`. The branch name for the release commit is `max/release`.

Create release commit and push to new branch:

```
$ git checkout -b max/release
$ git add pom.xml
$ git diff --staged
diff --git a/pom.xml b/pom.xml
index 2b71740..b126842 100644
--- a/pom.xml
+++ b/pom.xml
@@ -25,7 +25,7 @@ limitations under the License.
   </parent>
 
   <artifactId>mal-lib</artifactId>
-  <version>1.0.0-SNAPSHOT</version>
+  <version>1.0.0</version>
   <packaging>jar</packaging>
 
   <name>MAL lib</name>
@@ -58,7 +58,7 @@ limitations under the License.
   </developers>
 
   <scm>
-    <tag>HEAD</tag>
+    <tag>release/1.0.0</tag>
   </scm>
 
   <dependencies>
$ git commit -m "Release 1.0.0"
$ git push origin max/release
```

### 2. Make a pull request to `master` and get it approved and merged

Go to the repository on GitHub, click `Pull requests`, and then `New pull request`. Make sure that `base` is set to `master`, and set `compare` to your branch. Click `Create pull request`, add appropriate `Reviewers`, and add yourself as `Assignees`.

### 3. Make a release tag for the merged pull request

Once your pull request has been merged, you need to fetch the new merged commit in `master` to create the release tag:

```
$ git checkout master
$ git fetch
$ git merge --ff-only
$ git tag release/1.0.0
$ git push origin release/1.0.0
```

### 4. Make a snapshot commit and get it merged to `master`

Update `project.version` and `project.scm.tag` for the next snapshot. Following the examples above, `project.version` is set to `1.0.1-SNAPSHOT` and `project.scm.tag` is set to `HEAD`. The snapshot branch name is `max/snapshot` and the commit message is `Next snapshot`.

Get the snapshot commit merged as in step 2 above.

## License

Copyright © 2019-2021 [Foreseeti AB](https://foreseeti.com)

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
