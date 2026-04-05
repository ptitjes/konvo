To create a PNG-embedded character card, from a PNG image `card.png` and a JSON character card `card.json`,
use the following commands:

* For "chara" metadata:

    ```shell
    exiftool -config ./chara.config -chara="$(cat ./card.json | jq -c . | base64 -w0 -)" ./card.png
    ```

* For "ccv3" metadata:

    ```shell
    exiftool -config ./chara.config -ccv3="$(cat ./card.json | jq -c . | base64 -w0 -)" ./card.png
    ```

> Note: the `-config` option is required to use our custom `chara.config` file (available in this directory).
