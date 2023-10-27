import component.*
import kotlinx.browser.document
import kotlinx.css.*
import react.createElement
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
import styled.css

import styled.styledSpan
import wrappers.cReactQueryDevtools

val queryClient = QueryClient()

val styles = CssBuilder().apply {
    body {
        margin(0.px)
        padding(0.px)
    }

    h1 {
        fontSize = 20.pt
        fontWeight = FontWeight.normal
    }
}



fun main() {
    render(document.getElementById("root")!!) {
        HashRouter {
            QueryClientProvider {
                attrs.client = queryClient
                styledSpan {
                    css {
                        whiteSpace = WhiteSpace.preWrap
                        margin(top = 10.px, bottom = 10.px)
                    }
                    Link {
                        attrs.to = "/groups"
                        +"Группы"
                    }
                    +"   "
                    Link {
                        attrs.to = "/flows"
                        +"Потоки"
                    }
                }
                Routes {
                    Route {
                        attrs.path = "/groups"
                        attrs.element =
                            createElement(fcContainerGroupList())
                    }
                    Route {
                        attrs.path = "/groups/:groupId"
                        attrs.element =
                            createElement(fcContainerGroup())
                    }
                    Route {
                        attrs.path = "groups/:groupid/:subgroupname"
                        attrs.element =
                            createElement(fcContainerSubgroup())
                    }
                    Route {
                        attrs.path = "/flows"
                        attrs.element =
                            createElement(fcContainerFlowList())
                    }
                    Route {
                        attrs.path = "/flows/:flowId"
                        attrs.element =
                            createElement(fcContainerFlow())
                    }

                }
                child(cReactQueryDevtools()) {}
            }
        }
    }
}

