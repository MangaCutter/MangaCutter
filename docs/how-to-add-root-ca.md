# Installation of MangaCutter root CA

Installed root [Certificate Authority](https://en.wikipedia.org/wiki/Certificate_authority) is used by MangaCutter to
decrypt traffic that browser sends to Internet through MangaCutter. This is a core of image capturing module. So you
must install root CA in your browser if you want to download images from many sites.

![MangaCutter root CA must have exactly this fields](img/root-ca-x509-extensions.png)

## !!!WARNING!!!

Do not send your private part of root CA to someone(only Base64 string value in `Certificate Center` settings). Hackers
will be able to see all you actions in Internet if they will have your root CA as MangaCutter can. In case of any leak
distrust compromised CA immediately!!!

This is the reason why you have to generate CA locally. CA will be in safe if you do not have any viruses, because
MangaCutter or any another program do not send it to Internet.

## Common steps

1. Open MangaCutter.
2. Open `Plugin > Generate new certificate`
3. Select destination for public part of root CA aka Certificate file.
4. Now import this crt file to your browser, Details for well-known browsers below.
5. Now you can install MangaCutter Browser Adapter ([How-To](how-to-install-extension.md))

## Mozilla Firefox

5. Open [Privacy & Security](about:preferences#privacy) settings.
6. Click `View Certificates...` button.
   ![`View Certificates` button](img/firefox-ca-install-view-cert.png)
7. Go to `Authorities` tab.
   ![Certificates Manager](img/firefox-ca-install-cert-manager.png)
8. Click `Import` button and select previously created `crt` file.
9. Check `Trust this CA to identify web sites`.
    + (Optional) Compare SHA signature from MangaCutter notification and with SHA-1 in `View` tab. THEY MUST BE EQUAL.
10. Press `OK` button.
    ![Now you can see MangaCutter CA in this list](img/firefox-ca-install-result.png)
11. Congratulations! You have successfully imported MangaCutter root CA in Firefox.

## Opera and Google Chrome

5. Open `Certificates` window in browser.
    + In Opera: Go to [Security settings](opera://settings/security) and open `Manage certifiactes` link.
    + In Chrome: [Security settings](chrome://settings/security) and open `Manage certifiactes` link.
6. Go to `Trusted Root Certification Authorities` tab.
   ![Certificates](img/opera-chrome-ca-install-cert-manager.png)
7. Click `Import` and `Next` button.
8. Select previously created `crt` file and click `Next` button.
9. Select certificate destination as on the image below anc click `Next` button.
   ![Certificate destination](img/opera-chrome-ca-install-cert-location.png)
10. Verify fields and click `Finish` button.
    + (Optional)  Compare SHA signature from MangaCutter notification and with SHA-1 in `View` tab. THEY MUST BE EQUAL.
      ![Security warning](img/opera-chrome-ca-install-warning.png)
11. Press `Yes` and `OK` button.
    ![Now you can see MangaCutter CA in this list](img/opera-chrome-ca-install-result.png)
12. Congratulations! You have successfully imported MangaCutter root CA in Opera and Chrome.