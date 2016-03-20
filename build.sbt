name := "pg-uuids-v1-compare"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.postgresql"          %   "postgresql"            % "9.4-1201-jdbc41",
  "org.scalikejdbc"         %%  "scalikejdbc"           % "2.3.5",
  "org.scalikejdbc"         %%  "scalikejdbc-config"    % "2.3.5",
  "com.datastax.cassandra"  %   "cassandra-driver-core" % "3.0.0",
  "org.scalatest"           %%  "scalatest"             % "2.2.6"
)
