<#assign value = property.value>
<#assign keyValue = value.getKey()>
<#assign elementValue = value.getElement()>
<#assign elementTag = c2h.getCollectionElementTag(property)>

	<list name="${property.name}" inverse="${value.inverse?string}" 
	<#include "collection-tableattr.hbm.ftl"> 
	lazy="${c2h.getCollectionLazy(value)}"
	<#if property.cascade != "none">
        cascade="${property.cascade}"
	</#if>
	<#if !property.basicPropertyAccessor>
        access="${property.propertyAccessorName}"
	</#if>	
	<#if c2h.hasFetchMode(property)> fetch="${c2h.getFetchMode(property)}"</#if>>
		<#assign metaattributable=property>
		<#include "meta.hbm.ftl">
		<meta attribute="scope-get">@javax.persistence.OrderBy("id") public</meta>
		<#include "key.hbm.ftl">
		<list-index column="fooboo"/>
    		<#include "${elementTag}-element.hbm.ftl">
	</list>


