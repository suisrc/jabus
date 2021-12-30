.PHONY: start build

deps:
	cd start && mvn -Dverbose dependency:tree >__dependency.txt

clear:
	ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9

# mvn help:evaluate -o -Dexpression=project.version -q -DforceStdout
# mvn versions:revert
version:
	mvn versions:set -DnewVersion=${v}-SNAPSHOT

tag: # make tag v=1.0.1
	git tag v${v} && git push origin v${v}

tagn: # make tagn v=1.0.1
	git tag -d v${v} && git tag v${v} && git push origin v${v}

deploy: # make deploy v=1.0.1 修改版本 -> 提交更改 -> 上载 -> 打标签 -> 提交标签
	mvn versions:set -DnewVersion=${v}-SNAPSHOT && git add -A && git commit -am "update ${v}-SNAPSHOT" && git push && git tag v${v} && git push origin v${v}