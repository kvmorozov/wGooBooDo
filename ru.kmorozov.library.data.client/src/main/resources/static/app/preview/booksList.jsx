import React from "react";
import ReactGridLayout from "react-grid-layout";
import AutosizeInput from "react-input-autosize";

class BooksList extends React.Component {

    constructor(props) {
        super(props);

        this.update = this.update.bind(this);
    }

    update(books) {
        this.setState({books: books});
    }

    getLayoutItem(book, index) {
        return {
            i: book.id,
            x: index % 4,
            y: Math.floor(index / 4),
            w: 1, h: 1, static: true
        };
    }

    render() {
        if (this.state == null || this.state.books == null)
            return <ReactGridLayout></ReactGridLayout>
        else {
            let books = this.state.books;
            let layout = books.map((book, index) => this.getLayoutItem(book, index));

            let cells = books.map(book => <AutosizeInput key={book.id} value={book.title} onChange={()=> {
            }}/>);

            return (
                <ReactGridLayout className="layout" layout={layout}
                                 rowHeight={30} width={1000} cols={4}>
                    {cells}
                </ReactGridLayout>
            )
        }
    }
}

export default BooksList;