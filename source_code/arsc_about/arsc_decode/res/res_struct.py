#!/usr/bin/env python
# -*- coding: UTF-8 -*-

from struct import *
import re
import os

res_string_pool_chunk_offset = 0
package_chunk_offset = 0
key_string_pool_chunk_offset = 0
type_string_pool_chunk_offset = 0

res_type_offset = 0

type_string_list = []

package_id = 0;

class res_chunk_header(object):

	"""
		type h
		headerSize h
		size i

	"""
	header_size = 0
	size = 0
	header_type = 0x00

	"""docstring for ResChunkHeader"""
	def __init__(self):
		pass

	def parse(self,f):
		self.header_type = hex(unpack("h",f.read(2))[0])
		print 'res_chunk_header type: {}'.format(self.header_type)
		self.header_size = unpack("h",f.read(2))[0]
		print 'res_chunk_header header size: {}'.format(self.header_size)
		self.size = unpack("i",f.read(4))[0]
		print 'res_chunk_header size: {}'.format(self.size)
		return [self.header_size,self.size]


	def toString():
		pass

chunk_header = res_chunk_header()


class res_table_header(object):
	"""
		res_chunk_header
		packageCount i
	"""



	def __init__(self):
		pass

	def parse(self,f):
		print "==================== res_table_header start ===================="
		global res_string_pool_chunk_offset
		res_string_pool_chunk_offset = int(chunk_header.parse(f)[0])
		print 'package count: {}'.format(unpack("i",f.read(4))[0])
		print "==================== res_table_header end ======================"
		print "\n\r"

class res_string_pool_header(object):
	"""
		res_chunk_header
		string count i
		style count i
		flags i
		string start i
		style start i
	"""
	string_count = 0
	style_count = 0

	string_list = []
	style_list = []

	def __init__(self):
		pass

	def parse(self,f):
		print "=================== res_string_pool_header start ====================="
		global package_chunk_offset
		package_chunk_offset = res_string_pool_chunk_offset + int(chunk_header.parse(f)[1])

		self.string_count = unpack("i",f.read(4))[0]
		self.style_count = unpack("i",f.read(4))[0]
		print "string count : {}".format(self.string_count)
		print 'style count: {}'.format(self.style_count)
		print 'flags: {}'.format(unpack("i",f.read(4))[0])
		print 'string start: {}'.format(unpack("i",f.read(4))[0])
		print 'style start: {}'.format(unpack("i",f.read(4))[0])

		for i in xrange(0,self.string_count):
			self.string_list.append(unpack('i',f.read(4)))
		for i in xrange(0,self.style_count):
			self.style_list.append(unpack('i'),f.read(4))

		for i in xrange(0,self.string_count):
			
			size_char = bytes(f.read(2))[1]
			size = ord(size_char) & 0x7f
			if size != 0:
				val = ""
				val = f.read(size)
				print "str:{}".format(val)
			else:
				print "str:{}"
			f.read(1)

		print "=================== res_string_pool_header end ========================"
		print "\n\r"


class res_package(object):
	"""
		id i 4字节
		name  2 * 128
		typeStrins 4
		lastPublishType 4
		keyString 4
		lastPublicKey 4
		
	"""

	last_public_type = 0
	last_public_key = 0


	def __init__(self):
		pass

	def parse(self,f):
		print "========================= res_package start ========================="
		f.seek(package_chunk_offset)
		chunk_header.parse(f)
		global package_id
		package_id = unpack('i',f.read(4))[0]
		print "package id : {}".format(package_id)
		# todo 这里需要把对其做的填充数据去掉
		print "package name: {}".format(f.read(128 * 2))
		type_strings = unpack('i',f.read(4))[0]
		print "type string: {}".format(type_strings)
		self.last_public_type = unpack('i',f.read(4))[0]
		key_strings = unpack('i',f.read(4))[0]
		print "key string： {}".format(key_strings)
		self.last_public_key = unpack('i',f.read(4))[0]

		global key_string_pool_chunk_offset
		key_string_pool_chunk_offset = package_chunk_offset + key_strings
		global type_string_pool_chunk_offset
		type_string_pool_chunk_offset = package_chunk_offset + type_strings
		print "========================= res_package end   ========================="
		print "\n\r"
		

class res_type_string_pool(object):
	"""
	docstring for res_type_string_pool
		
		stringCount 4
		stylecount 4
		flags 4
		stringsstart 4
		stylesstart 4

	"""
	def __init__(self):
		pass

	def parse(self,f):
		print "======================== type_string_pool start ====================="
		f.seek(type_string_pool_chunk_offset)
		chunk_header.parse(f)
		string_count = unpack('i',f.read(4))[0]
		style_count = unpack('i',f.read(4))[0]
		flags = hex(unpack('i',f.read(4))[0])
		print "flags:{}".format(flags)
		f.read(4)
		f.read(4)

		string_list_index = []
		for i in xrange(0,string_count):
			string_list_index.append(unpack('i',f.read(4)))
		style_list_index = []
		for i in xrange(0,style_count):
			style_list_index.append(unpack('i',4))
		
		global type_string_list
		for i in xrange(0,string_count):
			size_char = bytes(f.read(2))[1]
			size = ord(size_char) & 0x7f
			if size != 0:
				val = f.read(size)
				type_string_list.append(val)
				print "str:{}".format(val)
			else:
				type_string_list.append("")
				print "str:"
			f.read(1)
		print "======================== type_string_pool end========================"
		print "\n\r"
		

class res_key_string_pool(object):
	"""
	docstring for res_key_string_pool
	和res_type_string_pool一样

	"""
	def __init__(self):
		pass
	def parse(self,f):
		print "======================== key_string_pool start ====================="
		f.seek(key_string_pool_chunk_offset)
		global res_type_offset
		res_type_offset = key_string_pool_chunk_offset + chunk_header.parse(f)[1]
		string_count = unpack('i',f.read(4))[0]
		style_count = unpack('i',f.read(4))[0]
		flags = hex(unpack('i',f.read(4))[0])
		print "flags:{}".format(flags)
		f.read(4)
		f.read(4)

		string_list = []
		for i in xrange(0,string_count):
			string_list.append(unpack('i',f.read(4)))
		style_list = []
		for i in xrange(0,style_count):
			style_list.append(unpack('i',4))
		
		for i in xrange(0,string_count):
			size_char = bytes(f.read(2))[1]
			size = ord(size_char) & 0x7f
			if size != 0:
				val = f.read(size)
				print "str:{}".format(val)
			else:
				print "str:"
			f.read(1)

		print "======================== key_string_pool end========================"
		print "\n\r"

		

def is_type_sped(f):
	chunk_header.parse(f)

def pasre_res_type_spec(f):
	global res_type_offset
	res_type_id = ord(bytes(f.read(1))[0]) & 0x7f
	res_0 = ord(bytes(f.read(1))[0]) & 0x7f
	res_1 = f.read(2)
	entry_count = unpack('i',f.read(4))[0]
	print "type name : {}".format(type_string_list[res_type_id-1])
	print "res type spec-----------> id:{0},res0:{1},res1:{2},entryCount:{3}".format(res_type_id,res_0,res_1,entry_count)
	offset = res_type_offset + chunk_header.header_size
	for i in xrange(0,entry_count):
		print "int element : {}".format(unpack('i',f.read(4)))
	res_type_offset += chunk_header.size

def parse_res_table_config(f):
	size = f.read(4)
	imsi = f.read(4)
	locale = f.read(4)
	screen_type = f.read(4)
	_input = f.read(4)
	screen_size = f.read(4)
	version = f.read(4)
	screen_config = f.read(4)
	screen_size_dp = f.read(4)
	locale_script = f.read(4)
	locale_varuant = f.read(8)
	print "size:{0},imsi:{1},local:{2},screen_type:{3}\
		_input:{4},screen_size:{5},version:{6},\
		screen_config:{7},screen_size_dp:{8},locale_script:{9}\
		locale_varuant:{10}\
		".format(size,imsi,locale,screen_type,_input,screen_size,version,screen_config,screen_size_dp,locale_script,
			locale_varuant)

def parse_res_entry(f,offset):
	f.seek(offset)
	entry_size = unpack('h',f.read(2))[0]
	entry_flag = unpack('h',f.read(2))[0]
	key_index = unpack('i',f.read(4))[0]
	return [entry_size,entry_flag,key_index]

def parse_entry_map(f,offset):
	f.seek(offset)
	size = unpack('h',f.read(2))[0]
	flags = unpack('h',f.read(2))[0]
	key = unpack('i',f.read(4))[0]
	parent = unpack('i',f.read(4))[0]
	count = unpack('i',f.read(4))[0]
	return [size,flags,key,parent,count]

def parse_res_table_map(f,offset):
	f.seek(offset)
	name = f.read(4)
	print "name:{}".format(name)
	parse_res_value(f,offset + 4)


def parse_res_value(f,offset):
	f.seek(offset)
	size = f.read(2)
	res0 = ord(bytes(f.read(1))[0]) & 0xFF
	data_type = ord(bytes(f.read(1))[0]) & 0xFF
	data = unpack('i',f.read(4))
	print "size:{0},res0:{1},data_type:{2},data:{3}".format(size,res0,data_type,data)

def parse_res_info(f):
	global package_id
	global res_type_offset
	res_type_id = ord(bytes(f.read(1))[0]) & 0x7f
	res_0 = ord(bytes(f.read(1))[0]) & 0x7f
	res_1 = f.read(2)
	entry_count = unpack('i',f.read(4))[0]
	entry_start = unpack('i',f.read(4))[0]
	parse_res_table_config(f)
	offset = res_type_offset +chunk_header.header_size
	f.seek(offset)
	print "type name:{}".format(type_string_list[res_type_id - 1])
	for i in xrange(0,entry_count):
		print "int element : {}".format(f.read(4))
	entry_offset = res_type_offset + entry_start
	value_offset = entry_offset
	body_size = 0
	for i in xrange(0,entry_count):
		res_id = hex(package_id << 24 | res_type_id << 16 | i & 0xFFFF)
		print "res id :{}".format(res_id)
		value_offset += body_size
		values = parse_res_entry(f,value_offset)
		size = values[0]
		if values[1] == 1:
			#tabmap entry
			entry_values = parse_entry_map(f,value_offset)
			for i in xrange(0,entry_values[4]):
				map_offset = value_offset + 16 + 12 * i
				parse_res_table_map(f,map_offset)
			body_size = 16 + 12 * entry_values[4]
		else:
			# res value 
			parse_res_value(f,value_offset + size)
			body_size = size + 8 
			
def parse_res(f,file_path):
	global res_type_offset
	f.seek(res_type_offset)
	while res_type_offset < os.path.getsize(file_path):
		chunk_header.parse(f)
		if chunk_header.header_type == '0x202':
			pasre_res_type_spec(f)
		else:
			parse_res_info(f)
			














