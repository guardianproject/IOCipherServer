
A secure on-device micro web server for accessing variety of files, including
IOCipher-based encrypted file systems, via a web HTML interface or a mountable
WebDav service.

* Does *NOT* require root or any special or extended privileges
* Includes ability to enable HTTPS/SSL support in server by autogenerating a
  local, self-signed SSL keypair and certificate.
* Uses mDNS to announce the availability of https and webdav(s) services
* Based on  the IOCipher framework: https://github.com/guardianproject/IOCipher
* Find out all about IOCipher here: https://guardianproject.info/code/iocipher/
* Please report bugs here: https://dev.guardianproject.info/projects/iocipher/issues
* We'd like to hear from you if you have included IOCipher in your app! Email
  us at root@guardianproject.info


Included Libraries
------------------

* Tiny Java Web Server http://tjws.sourceforge.net/
* Milton: Java WebDav API http://milton.ettrema.com/
* SpongyCastle (BouncyCastle repackaged) https://github.com/rtyley/spongycastle
* Simple Logging Facade for java http://www.slf4j.org/
* Apache Commons (IO, File Utils) http://commons.apache.org/
* JDOM http://jdom.org
