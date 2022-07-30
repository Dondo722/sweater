<#import "parts/common.ftl" as c>

<@c.page>
    <div class="form-row">
        <div class="form-group col-md-6">
            <form method="get" action="/main" class="form-inline">
                <input type="text" name="filter" class="form-control" value="${filter!}" placeholder="Search by tag"/>
                <button type="submit" class="btn btn-primary ml-2">Find</button>
            </form>
        </div>
    </div>
    <#if !user.isBanned()>
        <#include "parts/messageEdit.ftl" />
        <#else >
        <h5>Banned</h5>
    </#if>
    <#include "parts/messageList.ftl" />
</@c.page>