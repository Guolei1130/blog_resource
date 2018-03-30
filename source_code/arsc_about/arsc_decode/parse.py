#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import sys

from struct import *

import constants as constants
import res.res_struct as rs

def parseFile(params):
	if len(params) < 1:
		print "must have a file"
		exit(0)
	filePath = sys.argv[1]
	arscFile = open(filePath,"r+")

	# res_chunk_header = rs.res_chunk_header()
	# res_chunk_header.parse(arscFile)

	res_table_header = rs.res_table_header()
	res_table_header.parse(arscFile)	

	res_string_pool_header = rs.res_string_pool_header()
	res_string_pool_header.parse(arscFile)

	res_package = rs.res_package()
	res_package.parse(arscFile)

	res_type_string_pool = rs.res_type_string_pool()
	res_type_string_pool.parse(arscFile)

	res_key_string_pool = rs.res_key_string_pool()
	res_key_string_pool.parse(arscFile)

	rs.parse_res(arscFile,filePath)
		

	arscFile.close()
if __name__ == '__main__':
	
	parseFile(sys.argv)