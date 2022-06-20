<#import "parts/common.ftl" as c>
<@c.page>
    User editor
    <form action="/user" method="post">
        <div><label> User Name : <input type="text" name="username" value="${user.username}"/> </label></div>
        <#list roles as role>
            <div>
                <label>
                    <input type="checkbox" name="${role}" ${user.roles?seq_contains(role)?string("checked","")}/>${role}
                </label>
            </div>
        </#list>
        <input type="hidden" value="${user.id}" name="userId"/>
        <input type="hidden" name ="_csrf" value="${_csrf.token}"/>
        <div><button type="submit">Save</button></div>
    </form>
</@c.page>