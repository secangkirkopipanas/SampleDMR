#!/bin/bash
keytool -conf preconfig -genkeypair
keytool -conf preconfig -certreq
keytool -conf preconfig -list