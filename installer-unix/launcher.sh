#!/bin/bash
VERSION=1.1
DIR=./

if ! cat jcappstore-do-not-ask.info ; then
    if ! gpg --version > /dev/null 2&>1 ; then
        echo "You don't have GnuPG installed, the store will be unable to verify software integrity.\
	      this can be fixed anytime by GnuPG installation and JCAppStore key import."
        echo "Next time, the application will start WITHOUT THIS NOTICE!"
    else
        if ! gpg --list-keys 3D6FE2832EDFE9C9 > /dev/null 2&>1 ; then 
            echo "JCAppStore automatically verifies the software integrity for you. In order to do that" \
		      "we need to import our public key to your keyring and set the ultimate trust."
	    echo "You don't have to import the key, then the verification will not work. This can be" \
		      "changed anytime."
            echo "Do you wish to import y/n?"
	    read ANSWER
            while [ "$ANSWER" != "y" ] && [ "$ANSWER" != "n" ] ; do
                echo "Wrong answer. Try again:"
		read ANSWER
	    done
            if [[ "$ANSWER" == "y" ]] ; then
                gpg --import ${DIRECTORY}/store.asc
		(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 3D6FE2832EDFE9C9 trust
		echo "The key has been imported."
            else
		echo "The key can be imported anytime. Next time, the application will start WITHOUT THIS NOTICE!"
            fi
        fi
    fi
    touch jcappstore-do-not-ask.info
fi
cd ${DIR}
java -jar ./JCAppStore-${VERSION}.jar
