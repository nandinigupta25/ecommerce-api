#!/bin/sh
# Maven wrapper script
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(pwd)}"
exec "$JAVA_HOME/bin/java" \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
