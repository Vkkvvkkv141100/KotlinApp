import component.*
import kotlinx.browser.document
import react.createElement
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
import wrappers.cReactQueryDevtools

val queryClient = QueryClient()

fun main() {
    render(document.getElementById("root")!!) {
        HashRouter {
            QueryClientProvider {
                attrs.client = queryClient
                Link {
                    attrs.to = "/"
                    +"Главная"
                }
//                +"⠀⠀⠀⠀" //invisible symbols -- unicode/U+2800
//                Link {
//                    attrs.to = "/students"
//                    +"Студенты"
//                }
//                +"⠀⠀⠀⠀"
//                Link {
//                    attrs.to = "/groups"
//                    +"Группы"
//                }
                +"⠀⠀⠀⠀"
                Link {
                    attrs.to = "/teachers"
                    +"Преподавтели"
                }
                +"⠀⠀⠀⠀"
                Link {
                    attrs.to = "/lessons"
                    +"Занятия"
                }
                Routes {
//                    Route {
//                        attrs.index = true
//                        attrs.path = "/students"
//                        attrs.element = createElement(fcContainerStudentList())
//                    }
//                    Route {
//                        attrs.path = "/students/:id"
//                        attrs.element = createElement(fcContainerStudent())
//                    }
//                    Route {
//                        attrs.index = true
//                        attrs.path = "/groups"
//                        attrs.element = createElement(fcContainerGroupList())
//                    }
//                    Route {
//                        attrs.path = "/groups/:group"
//                        attrs.element = createElement(fcContainerGroup())
//                    }
                    Route {
                        attrs.index = true
                        attrs.path = "/teachers"
                        attrs.element = createElement(fcContainerTeacherList())
                    }
                    Route {
                        attrs.path = "/teachers/:id"
                        attrs.element = createElement(fcContainerTeacher())
                    }
                    Route {
                        attrs.index = true
                        attrs.path = "/lessons"
                        attrs.element = createElement(fcContainerLessonList())
                    }
                    Route {
                        attrs.path = "/lessons/:id/details"
                        attrs.element = createElement(fcContainerLesson())
                    }
                }
                child(cReactQueryDevtools()) {}
                //disabled in React production build
                //see notes in "/wrappers/react-query-devtools.kt"
            }
        }
    }
}
