<#macro pager url page>
    <#if (page.totalPages > 7) >
        <#assign
            totalPages = page.totalPages
            pageNumber = page.number + 1

            head = (pageNumber > 4)?then([1,-1],[1,2,3])
            tail = (pageNumber < totalPages - 3)?then([-1, totalPages], [totalPages - 2 ,totalPages - 1,totalPages])
            bodyBefore = (pageNumber > 4 && pageNumber < totalPages -1)?then([pageNumber-2, pageNumber-1],[])
            bodyAfter = (pageNumber > 2 && pageNumber < totalPages -3)?then([pageNumber+1, pageNumber+2],[])

            body = head + bodyBefore
            +(pageNumber > 3 && pageNumber < totalPages -3)?then([pageNumber],[])
            + bodyAfter + tail
        />
    <#else>
        <#assign body = 1..page.totalPages/>
    </#if>

    <div class="mt-3">
        <ul class="pagination">
            <li class="page-item disabled">
                <a class="page-link" href="#" tabindex="-1">Pages</a>
            </li>
            <#list body as p>
                <#if page.number == (p - 1)>
                    <li class="page-item active">
                        <a class="page-link" href="#" tabindex="-1">${p}</a>
                    </li>
                <#elseif p ==-1>
                    <li class="page-item disabled">
                        <a class="page-link" href="#" tabindex="-1">...</a>
                    </li>
                <#else>
                    <li class="page-item">
                        <a class="page-link" href="${url}?page=${p - 1}&size=${page.size}" tabindex="-1">${p}</a>
                    </li>
                </#if>
            </#list>
        </ul>
    </div>
    <div class="mt-3">
        <ul class="pagination">
            <li class="page-item disabled">
                <a class="page-link" href="#" tabindex="-1">Elements</a>
            </li>
            <#list [3,6,9,18] as c>
                <#if page.size == c>
                    <li class="page-item active">
                        <a class="page-link" href="#" tabindex="-1">${c}</a>
                    </li>
                <#else>
                    <li class="page-item">
                        <a class="page-link" href="${url}?page=${page.number}&size=${c}" tabindex="-1">${c}</a>
                    </li>
                </#if>
            </#list>
        </ul>
    </div>
</#macro>