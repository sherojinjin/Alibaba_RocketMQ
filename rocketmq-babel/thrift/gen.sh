#!/bin/sh

thrift  -r --gen java Model.thrift
thrift  -r --gen py Model.thrift
thrift  -r --gen php Model.thrift

thrift  -r --gen java  Consumer.thrift
thrift  -r --gen py  Consumer.thrift
thrift  -r --gen php  Consumer.thrift

thrift  -r --gen java Producer.thrift
thrift  -r --gen py Producer.thrift
thrift  -r --gen php Producer.thrift

