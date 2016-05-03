#!/bin/sh
VERSION="1.0.2"
cp metrics-reporting-1.0.pom dist/lib/metrics-reporting-${VERSION}.pom
cd dist/lib
gpg -ab metrics-reporting-${VERSION}.pom
gpg -ab metrics-reporting-${VERSION}.jar
gpg -ab metrics-reporting-${VERSION}-sources.jar
gpg -ab metrics-reporting-${VERSION}-javadoc.jar
jar -cvf ../bundle.jar *

