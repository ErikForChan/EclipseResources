-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.cpp
%options template=LRSecondaryParserTemplate.g

$Import
	CPPGrammar.g
$DropRules

	cast_expression
	    ::= '(' type_id ')' cast_expression

$End

$Define
    $ast_class /. IASTExpression ./
$End

$Start
    no_cast_start
$End

$Rules 

	no_cast_start
	    ::= expression
	      | ERROR_TOKEN
	          /. $Build  consumeEmpty();  $EndBuild ./
          
$End