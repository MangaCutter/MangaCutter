# Installation of MangaCutter root certificate

Installed [Root certificate](https://en.wikipedia.org/wiki/Root_certificate) is used by MangaCutter to decrypt traffic
that browser sends to Internet through MangaCutter. This is a core of image capturing module. So you must install root
certificate in your browser if you want to download images from many sites.

![MangaCutter root certificate must have exactly this fields](img/root-ca-x509-extensions.png)

## !!!WARNING!!!

Do not send your private part of root certificate to someone(only Base64 string value in `Certificate Center` settings).
Hackers will be able to see all you actions in Internet if they will have your root certificate as MangaCutter can. In
case of any leak distrust compromised root certificate immediately!!!

This is the reason why you have to generate root certificate locally. Root certificate will be in safe if you do not
have any viruses, because MangaCutter or any another common program can not send it to Internet.

## Common steps

1. Open MangaCutter.
2. Open `Plugin > Generate new certificate`
3. Select destination for public part of root certificate.
4. Now import created file to your browser. Details for well-known browsers below.
5. Now you can install MangaCutter Browser Adapter ([How-To](how-to-install-extension.md))

## Mozilla Firefox

5. Open [Privacy & Security](about:preferences#privacy) settings.
6. Click `View Certificates...` button.

   ![`View Certificates...` button](img/firefox-ca-install-view-cert.png)
7. Go to `Authorities` tab.

   ![Certificates Manager](img/firefox-ca-install-cert-manager.png)
8. Click `Import` button and select previously created crt-file.
9. Check `Trust this CA to identify web sites`.

   ![Should be as here](img/firefox-ca-install-checkbox.png)
    + (Optional) Compare SHA-1 signature from MangaCutter notification and with SHA-1 in `View` tab. THEY MUST BE EQUAL.
10. Press `OK` button.

    ![Now you can see MangaCutter root certificate in list](img/firefox-ca-install-result.png)
11. Congratulations! You have successfully imported MangaCutter root certificate in Firefox.

## Opera and Google Chrome

5. Open `Certificates` window in browser.
    + In Opera: Go to [Security settings](opera://settings/security) and open `Manage certifiactes` link.
    + In Chrome: [Security settings](chrome://settings/security) and open `Manage certifiactes` link.
6. Go to `Trusted Root Certification Authorities` tab.

   ![Certificates](img/opera-chrome-ca-install-cert-manager.png)
7. Click `Import` and `Next` button.
8. Select previously created crt-file and click `Next` button.
9. Select certificate destination as on the image below anc click `Next` button.

   ![Certificate destination](img/opera-chrome-ca-install-cert-location.png)
10. Verify fields and click `Finish` button.
    + (Optional) Compare SHA-1 signature from MangaCutter notification and with SHA-1 in shown notification tab. THEY
      MUST BE EQUAL.

      ![Security warning](img/opera-chrome-ca-install-warning.png)
11. Press `Yes` and `OK` button.

    ![Now you can see MangaCutter root certificate in this list](img/opera-chrome-ca-install-result.png)

12. Congratulations! You have successfully imported MangaCutter root certificate in Opera and Chrome.