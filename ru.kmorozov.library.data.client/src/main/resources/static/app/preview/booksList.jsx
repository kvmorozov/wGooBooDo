import React from "react";
import {Button} from "semantic-ui-react";
import {BootstrapTable, TableHeaderColumn} from "react-bootstrap-table";
import BookPreviewPopup from "./bookPreviewPopup";

class BooksList extends React.Component {

    constructor(props) {
        super(props);
    }

    update = (books) => {
        this.setState({books: books});
    }

    handleItemClick = (row) => {
        const selectedBook = this.state.books.find(book => book.id == row.id);
        this.refs.bookPreviewPopup.open(selectedBook);
    }

    getDisplayItem(book) {
        let imgPath;
        switch (book.format) {
            case 'PDF':
                imgPath = '/icons/pdf.png';
                break;
            case 'DJVU':
                imgPath = '/icons/djvu.png';
                break;
        }

        return (
            <Button onClick={this.handleItemClick(book)}>Preview</Button>
        )
    }

    render() {
        if (this.state == null || this.state.books == null)
            return <div/>
        else {
            const options = {
                onRowClick: this.handleItemClick,
                onRowDoubleClick: this.handleItemClick
            };

            return (
                <div>
                    <BootstrapTable ref='table' data={ this.state.books } options={ options }
                                    pagination>
                        <TableHeaderColumn dataField='id' isKey={ true }>Book ID</TableHeaderColumn>
                        <TableHeaderColumn dataField='format'>Format</TableHeaderColumn>
                        <TableHeaderColumn dataField='title'>Title</TableHeaderColumn>
                    </BootstrapTable>
                    <BookPreviewPopup ref="bookPreviewPopup"/>
                </div>
            )
        }
    }
}

export default BooksList;