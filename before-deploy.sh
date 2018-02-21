#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_0c244a1263e9_key -iv $encrypted_0c244a1263e9_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import --no-tty --yes codesigning.asc
fi