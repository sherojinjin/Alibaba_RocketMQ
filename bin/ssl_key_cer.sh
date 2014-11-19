#!/bin/sh
openssl genrsa -out private.pem 2048
openssl req -new -key private.pem -out cert.csr
openssl req -new -x509 -key private.pem -out cer.pem -days 1095
