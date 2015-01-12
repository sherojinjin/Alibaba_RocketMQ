#!/bin/sh
thrift --gen java  Consumer.thrift
thrift --gen py  Consumer.thrift
thrift --gen php  Consumer.thrift

thrift --gen java Producer.thrift
thrift --gen py Producer.thrift
thrift --gen php Producer.thrift
